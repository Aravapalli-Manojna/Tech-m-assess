Dependencies(Maven pom.xml) 
 
<dependencies> 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-web</ar factId> 
    </dependency> 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-data-jpa</ar factId> 
    </dependency> 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-security</ar factId> 
    </dependency> 
    <dependency> 
        <groupId>mysql</groupId> 
        <ar factId>mysql-connector-java</ar factId> 
    </dependency> 
    <!-- For searching (op onal but recommended) --> 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-valida on</ar factId> 
    </dependency> 
</dependencies> 
 
 
@En ty 
public class Product { 
    @Id 
    @GeneratedValue(strategy = Genera onType.IDENTITY) 
    private Long productId; 
 
    private String name; 
    private String descrip on; 
    private Double price; 
    private String category; 
    private Integer stockQuan ty; 
} 
 
 
@Repository 
public interface ProductRepository extends JpaRepository<Product, Long> { 
    List<Product> findByCategory(String category); 
    List<Product> findByPriceBetween(Double min, Double max); 
    List<Product> findByNameContainingIgnoreCase(String name); 
} 
 
@Service 
public class ProductService { 
    @Autowired 
    private ProductRepository repository; 
 
    public List<Product> getAllProducts() { 
        return repository.findAll(); 
    } 
 
    public Op onal<Product> getProduct(Long id) { 
        return repository.findById(id); 
    } 
 
    public Product addProduct(Product product) { 
        return repository.save(product); 
    } 
 
    public Product updateProduct(Long id, Product updatedProduct) { 
        Product product = repository.findById(id).orElseThrow(); 
        product.setName(updatedProduct.getName()); 
        product.setPrice(updatedProduct.getPrice()); 
        product.setDescrip on(updatedProduct.getDescrip on()); 
        product.setCategory(updatedProduct.getCategory()); 
        product.setStockQuan ty(updatedProduct.getStockQuan ty()); 
        return repository.save(product); 
    } 
 
    public void deleteProduct(Long id) { 
        repository.deleteById(id); 
    } 
 
    public List<Product> filterByCategory(String category) { 
        return repository.findByCategory(category); 
    } 
 
    public List<Product> filterByPriceRange(Double min, Double max) { 
        return repository.findByPriceBetween(min, max); 
    } 
 
    public List<Product> searchByName(String name) { 
        return repository.findByNameContainingIgnoreCase(name); 
    } 
} 
 
 
@RestController 
@RequestMapping("/api/products") 
public class ProductController { 
 
    @Autowired 
    private ProductService service; 
 
    @GetMapping 
    public List<Product> getAllProducts() { 
        return service.getAllProducts(); 
    } 
 
    @GetMapping("/{id}") 
    public ResponseEn ty<Product> getProduct(@PathVariable Long id) { 
        return service.getProduct(id) 
                .map(ResponseEn ty::ok) 
                .orElse(ResponseEn ty.notFound().build()); 
    } 
 
    @PostMapping 
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEn ty<Product> addProduct(@RequestBody @Valid Product product) { 
        return new ResponseEn ty<>(service.addProduct(product), H pStatus.CREATED); 
    } 
 
    @PutMapping("/{id}") 
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEn ty<Product> updateProduct(@PathVariable Long id, @RequestBody 
Product product) { 
        return ResponseEn ty.ok(service.updateProduct(id, product)); 
    } 
 
    @DeleteMapping("/{id}") 
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEn ty<Void> deleteProduct(@PathVariable Long id) { 
        service.deleteProduct(id); 
        return ResponseEn ty.noContent().build(); 
    } 
 
    @GetMapping("/search") 
    public List<Product> searchProducts(@RequestParam String name) { 
        return service.searchByName(name); 
    } 
 
    @GetMapping("/filter") 
    public List<Product> filterProducts(@RequestParam(required = false) String category, 
                                        @RequestParam(required = false) Double minPrice, 
                                        @RequestParam(required = false) Double maxPrice) { 
        if (category != null) { 
            return service.filterByCategory(category); 
        } else if (minPrice != null && maxPrice != null) { 
            return service.filterByPriceRange(minPrice, maxPrice); 
        } 
        return service.getAllProducts(); 
    } 
} 
 
 
@EnableWebSecurity 
public class SecurityConfig { 
 
    @Bean 
    public SecurityFilterChain filterChain(H pSecurity h p) throws Excep on { 
        h p.csrf().disable() 
            .authorizeH pRequests() 
            .requestMatchers(H pMethod.POST, "/api/products/**").hasRole("ADMIN") 
            .requestMatchers(H pMethod.PUT, "/api/products/**").hasRole("ADMIN") 
            .requestMatchers(H pMethod.DELETE, "/api/products/**").hasRole("ADMIN") 
            .anyRequest().authen cated() 
            .and() 
            .h pBasic(); 
 
        return h p.build(); 
    } 
 
    @Bean 
    public UserDetailsService users() { 
        UserDetails admin = User.builder() 
            .username("admin") 
            .password("{noop}admin123") // {noop} means no encoding for simplicity 
            .roles("ADMIN") 
            .build(); 
 
        UserDetails user = User.builder() 
            .username("user") 
            .password("{noop}user123") 
            .roles("USER") 
            .build(); 
 
        return new InMemoryUserDetailsManager(admin, user); 
    } 
} 
 
Applica on Proper es :  
 
spring.datasource.url=jdbc:mysql://localhost:3306/product_catalog 
spring.datasource.username=root 
spring.datasource.password=your_password 
spring.jpa.hibernate.ddl-auto=update 
spring.jpa.show-sql=true 
 
 
@RestControllerAdvice 
public class GlobalExcep onHandler { 
 
    @Excep onHandler(NoSuchElementExcep on.class) 
    public ResponseEn ty<String> handleNotFound(NoSuchElementExcep on ex) { 
        return ResponseEn ty.status(H pStatus.NOT_FOUND).body("Product not found."); 
    } 
 
    @Excep onHandler(Excep on.class) 
    public ResponseEn ty<String> handleGeneric(Excep on ex) { 
        return ResponseEn ty.status(H pStatus.INTERNAL_SERVER_ERROR).body("An error 
occurred."); 
    } 
} 