package com.business.business.product;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.business.business.auth.AuthService;
import com.business.business.category.Category;
import com.business.business.category.CategoryService;
import com.business.business.exception.BadRequestException;
import com.business.business.store.Store;
import com.business.business.tag.Tag;
import com.business.business.tag.TagService;
import com.business.business.user.Role;
import com.business.business.user.User;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final EntityManager em;
    private final EntityViewManager evm;
    private final CriteriaBuilderFactory cbf;

    public Product createProduct(@Valid ProductDto productDto) {
        Product product = mapFromDtoToProduct(productDto);

        System.out.println(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("d/MM/uuuu")));
        return productRepository.save(product);
    }

    private Product mapFromDtoToProduct(ProductDto productDto) {
        Category category = null;
        if (productDto.categoryId != null){
            category = categoryService.getCategoryById(productDto.categoryId);
        }

        Store store = AuthService.getCurrentAuthenticatedUserStore();
        if (store==null){
            throw new BadRequestException("You cannot create a product since you don't have a store");
        }

//        Set<Tag> tags = null;
//        if (productDto.tags != null){
//            tags = tagService.findAllByName(productDto.tags);
//        }

        Product product = Product.builder()
                .name(productDto.name)
                .category(category)
                .imageUrl(productDto.imageUrl)
//                .tags(tags)
                .store(store)
                .numberAvailable(productDto.numberAvailable)
                .costPrice(productDto.costPrice)
                .sellingPrice(productDto.sellingPrice)
                .description(productDto.description)
                .build();
        return product;
    }

    public List<Product> createProducts(List<ProductDto> productDtos) {
        List<Product> products = productDtos.stream().map(this::mapFromDtoToProduct).collect(Collectors.toList());
        return productRepository.saveAll(products);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<ProductShortView> filter(UUID categoryId, String search, boolean searchTags) {
        CriteriaBuilder<Product> productCriteriaBuilder = cbf.create(em, Product.class);

        if (categoryId!=null){
            productCriteriaBuilder.where("category.id").eq(categoryId);
        }

        User currentAuthUser = AuthService.getCurrentAuthenticatedUser();
        if (currentAuthUser.getRole() != Role.ADMIN && currentAuthUser.getStore() != null){
            productCriteriaBuilder.where("store.id").eq(currentAuthUser.getStore().id);
        }

        if (search!=null){
            String likeValue = "%" + search.toLowerCase() + "%";
            WhereOrBuilder<CriteriaBuilder<Product>> orBuilder = productCriteriaBuilder.whereOr()
                    .where("LOWER(name)").like().value(likeValue).noEscape()
                    .where("LOWER(category.name)").like().value(likeValue).noEscape();
            if (searchTags) orBuilder.where("tags.name").like().value(likeValue).noEscape();
            orBuilder.endOr();
        }
        productCriteriaBuilder.distinct();
        CriteriaBuilder<ProductShortView> productViewCb = evm.applySetting(EntityViewSetting.create(ProductShortView.class), productCriteriaBuilder);

        return productViewCb.getResultList();
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public Product updateProduct(UUID id, @Valid ProductDto productDto) {
        Product product = getProductById(id);

        Store store = AuthService.getCurrentAuthenticatedUserStore();
        if (store==null){
            throw new BadRequestException("You cannot update a product since you don't have a store");
        }

        if (!product.store.equals(store)){
            throw new BadRequestException("You cannot update a product that is not from your store");
        }

        if (productDto.categoryId != null){
            Category category = categoryService.getCategoryById(productDto.categoryId);
            product.setCategory(category);
        }

//        Set<Tag> tags = null;
//        if (productDto.tags != null){
//            tags = tagService.findAllByName(productDto.tags);
//        }

        if (Strings.isNotBlank(productDto.name)){
            product.setName(productDto.name);
        }

        if (Strings.isNotBlank(productDto.imageUrl)) {
            product.setImageUrl(productDto.imageUrl);
        }
//        product.setTags(tags);

        if (productDto.numberAvailable != null && productDto.numberAvailable<0) {
            product.setNumberAvailable(productDto.numberAvailable);
        }
        if (productDto.costPrice!=null) {
            product.setCostPrice(productDto.costPrice);
        }

        return productRepository.save(product);
    }

    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public Product updateProductImage(UUID id, String imageUrl) {
        Product productById = getProductById(id);
        productById.setImageUrl(imageUrl);
        return productRepository.save(productById);
    }
}
