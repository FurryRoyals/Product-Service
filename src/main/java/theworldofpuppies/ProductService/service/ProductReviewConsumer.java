package theworldofpuppies.ProductService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theworldofpuppies.ProductService.exception.ResourceNotFoundException;
import theworldofpuppies.ProductService.model.Product;
import theworldofpuppies.ProductService.model.ReviewEvent;
import theworldofpuppies.ProductService.repository.ProductRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductReviewConsumer {

    private final ProductRepository productRepository;

    // Listen to product reviews (sent when TargetType is not BOOKING)
    @Transactional
    @KafkaListener(topics = "reviews.created.order", groupId = "product-service-group")
    public void consumeProductReview(ReviewEvent event) {
        try {
            Product product = productRepository.findById(event.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with this id: " + event.getId()));
            int newTotalReviews = product.getTotalReviews() + 1;
            Double newAverageStars = (product.getAverageStars() + event.getStars()) / newTotalReviews;
            product.setTotalReviews(newTotalReviews);
            product.setAverageStars(newAverageStars);
            product.setIsRated(true);
            productRepository.save(product);
            log.info("Received product review: {}", event);
            // process the product review notification
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}