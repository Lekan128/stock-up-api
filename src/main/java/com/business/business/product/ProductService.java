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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.lekan128.aiagent.api.Agent;
import io.github.lekan128.aiagent.api.annotation.AiToolMethod;
import io.github.lekan128.aiagent.api.annotation.ArgDesc;
import io.github.lekan128.aiagent.api.llm.Gemini;
import io.github.lekan128.aiagent.core.AgentProvider;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
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

    public List<Product> createProductsWithSmallDto(@Valid List<ProductShortDto> productSmallDtos) {
        Set<Product> products = productSmallDtos.stream().map(this::mapFromDtoToProduct).collect(Collectors.toSet());
        return productRepository.saveAll(products);
    }

    @Async //run method asynchronously
    @Transactional(propagation = Propagation.REQUIRES_NEW) //create a new thread for it
    public void asynchronouslyGetAndSaveProductDescription(List<Product> products){
        try {
            List<ProductInfo> productInfos = getDescriptionFromAi(products);

            Map<UUID, ProductInfo> descriptionMap = productInfos.stream()
                    .collect(Collectors.toMap(
                            ProductInfo::productId,
                            productInfo -> productInfo
                    ));
            productInfos.forEach(productInfo -> descriptionMap.put(productInfo.productId(), productInfo));

            for (Product product : products){
                ProductInfo productInfo = descriptionMap.get(product.getId());
                String imageUrl = productInfo.imageUrl();
                String description = productInfo.productDescription();
                if (description != null) {
                    if (description.length() > 500) description = description.substring(0, 500) + "...";
                    product.setDescription(description);
                }
                if (imageUrl!=null){
                    product.setImageUrl(imageUrl);
                }
            }

            productRepository.saveAll(products);
            log.info("Product description gotten and products saved.");
        } catch (JsonProcessingException e) {
            log.error("Error getting AI description and saving it: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private List<ProductInfo> getDescriptionFromAi(List<Product> products) throws JsonProcessingException {
        Gemini gemini = new Gemini();

        ObjectMapper objectMapper = new ObjectMapper();
        String productsString = objectMapper
                .registerModule(new JavaTimeModule())
                .writerWithDefaultPrettyPrinter().writeValueAsString(products);

        String query = "Get me information about the products: " + productsString;
        String aiPersona = "An expert product describer." +
                "The description of the product must be short and should contain header(s) with bullet points";

        Agent agent = AgentProvider.get();

        return agent.useAgent(query, aiPersona, gemini, new TypeReference<List<ProductInfo>>() {
        });
    }

    @AiToolMethod("Help to update existing products. It is important that the id is consistent with the product's id")
    private Product updateProduct(@ArgDesc("The product to will be updated") Product product){
       return productRepository.save(product);
    }

    private Product mapFromDtoToProduct(ProductShortDto productDto) {
        Store store = AuthService.getCurrentAuthenticatedUserStore();
        if (store==null){
            throw new BadRequestException("You cannot create a product since you don't have a store");
        }
        return Product.builder()
                .name(productDto.name)
                .numberAvailable(productDto.numberAvailable)
                .costPrice(productDto.costPrice)
                .sellingPrice(productDto.sellingPrice)
                .store(store)
                .build();
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

    @AiToolMethod("Get all products the current user")
    public List<Product> getAllProducts() {
        User currentAuthUser = AuthService.getCurrentAuthenticatedUser();
        if (currentAuthUser.getRole() != Role.ADMIN){
            if (currentAuthUser.getStore() == null){
                throw new BadRequestException("You need to have a shop to access this endpoint.");
            }
            UUID storeId = currentAuthUser.getStore().id;
            return productRepository.findAllByStore_Id(storeId);
        }
        return productRepository.findAll();
    }

    @AiToolMethod("Search for a particular product")
    public List<ProductShortView> filter(
            @Nullable @ArgDesc("product category id") UUID categoryId,
            @ArgDesc("The search word") String search,
            @ArgDesc("if product tag should be included in the tag")boolean searchTags
    ) {
        CriteriaBuilder<Product> productCriteriaBuilder = cbf.create(em, Product.class);

        if (categoryId!=null){
            productCriteriaBuilder.where("category.id").eq(categoryId);
        }

        User currentAuthUser = AuthService.getCurrentAuthenticatedUser();

        if (currentAuthUser.getRole() != Role.ADMIN && currentAuthUser.getStore() == null){
            throw new BadRequestException("User does not have a shop");
        }
        if (currentAuthUser.getRole() != Role.ADMIN){
            productCriteriaBuilder.where("store.id").eq(currentAuthUser.getStore().id);
        }

        if (currentAuthUser.getRole() != Role.ADMIN){
            if (currentAuthUser.getStore() == null){
                throw new BadRequestException("You need to have a shop to access this endpoint.");
            }
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
        Product product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        return product;
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

        if (productDto.tags != null){
            Set<Tag> tags = tagService.findAllByName(productDto.tags);
            product.setTags(tags);
        }

        if (Strings.isNotBlank(productDto.description)){
            product.setDescription(productDto.description);
        }

        if (Strings.isNotBlank(productDto.name)){
            product.setName(productDto.name);
        }

        if (Strings.isNotBlank(productDto.imageUrl)) {
            product.setImageUrl(productDto.imageUrl);
        }

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
