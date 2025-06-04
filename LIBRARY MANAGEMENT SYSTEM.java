Dependencies(Maven pom.xml) 
<dependencies> 
<dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-data-jpa</ar factId> 
    </dependency> 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-security</ar factId> 
    </dependency> 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-web</ar factId> 
    </dependency> 
 
    <dependency> 
        <groupId>com.h2database</groupId> 
        <ar factId>h2</ar factId> 
    </dependency> 
 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-valida on</ar factId> 
    </dependency> 
</dependencies> 
 
 
@En ty 
public class Book { 
    @Id 
    @GeneratedValue(strategy = Genera onType.IDENTITY) 
    private Long bookId; 
 
    private String tle; 
    private String author; 
    private String category; 
    private boolean availability = true; 
} 
 
 
@En ty 
public class User { 
    @Id 
    @GeneratedValue(strategy = Genera onType.IDENTITY) 
    private Long userId; 
 
    private String name; 
    private String membershipType; 
} 
 
 
@En ty 
public class Transac on { 
    @Id 
    @GeneratedValue(strategy = Genera onType.IDENTITY) 
    private Long transac onId; 
 
    @ManyToOne 
    private Book book; 
 
    @ManyToOne 
    private User user; 
 
    private LocalDate issueDate; 
    private LocalDate returnDate; 
    private String status; // e.g., ISSUED, RETURNED, OVERDUE 
} 
 
 
public interface BookRepository extends JpaRepository<Book, Long> { 
    List<Book> 
findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnore
 Case(String tle, String author, String category); 
} 
 
public interface UserRepository extends JpaRepository<User, Long> {} 
 
public interface Transac onRepository extends JpaRepository<Transac on, Long> { 
    List<Transac on> findByStatus(String status); 
    List<Transac on> findByUserUserId(Long userId); 
} 
 
 
@Service 
public class BookService { 
    @Autowired 
    private BookRepository repo; 
 
    public Book add(Book book) { return repo.save(book); } 
 
    public void delete(Long id) { repo.deleteById(id); } 
 
    public List<Book> search(String keyword) { 
        return 
repo.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingI
 gnoreCase(keyword, keyword, keyword); 
    } 
 
    public Op onal<Book> get(Long id) { return repo.findById(id); } 
} 
 
 
@Service 
public class Transac onService { 
    @Autowired 
    private Transac onRepository txRepo; 
    @Autowired 
    private BookRepository bookRepo; 
 
    public Transac on issueBook(Long bookId, User user) { 
        Book book = bookRepo.findById(bookId).orElseThrow(); 
        if (!book.isAvailability()) throw new RunmeExcep on("Book not available"); 
 
        book.setAvailability(false); 
        bookRepo.save(book); 
 
        Transac on tx = new Transac on(); 
        tx.setBook(book); 
        tx.setUser(user); 
        tx.setIssueDate(LocalDate.now()); 
        tx.setStatus("ISSUED"); 
 
        return txRepo.save(tx); 
    } 
 
    public Transac on returnBook(Long transac onId) { 
        Transac on tx = txRepo.findById(transac onId).orElseThrow(); 
        tx.setReturnDate(LocalDate.now()); 
        tx.setStatus("RETURNED"); 
 
        Book book = tx.getBook(); 
        book.setAvailability(true); 
        bookRepo.save(book); 
 
        return txRepo.save(tx); 
    } 
 
    public List<Transac on> getOverdueBooks() { 
        return txRepo.findAll().stream() 
            .filter(tx -> "ISSUED".equals(tx.getStatus()) && 
                    tx.getIssueDate().isBefore(LocalDate.now().minusDays(14))) 
            .peek(tx -> tx.setStatus("OVERDUE")) 
            .toList(); 
    } 
 
    public List<Transac on> userHistory(Long userId) { 
        return txRepo.findByUserUserId(userId); 
    } 
} 
 
 
@RestController 
@RequestMapping("/books") 
public class BookController { 
    @Autowired 
    private BookService service; 
 
    @PostMapping 
    @PreAuthorize("hasRole('LIBRARIAN')") 
    public Book add(@RequestBody Book book) { 
        return service.add(book); 
    } 
 
    @DeleteMapping("/{id}") 
    @PreAuthorize("hasRole('LIBRARIAN')") 
    public void delete(@PathVariable Long id) { 
        service.delete(id); 
    } 
 
    @GetMapping("/search") 
    public List<Book> search(@RequestParam String keyword) { 
        return service.search(keyword); 
    } 
 
    @GetMapping("/{id}") 
    public Book get(@PathVariable Long id) { 
        return service.get(id).orElseThrow(); 
    } 
} 
 
 
@RestController 
@RequestMapping("/transac ons") 
public class Transac onController { 
    @Autowired 
    private Transac onService txService; 
    @Autowired 
    private UserRepository userRepo; 
 
    @PostMapping("/issue") 
    @PreAuthorize("hasRole('LIBRARIAN')") 
    public Transac on issue(@RequestParam Long bookId, @RequestParam Long userId) { 
        User user = userRepo.findById(userId).orElseThrow(); 
        return txService.issueBook(bookId, user); 
    } 
 
    @PostMapping("/return") 
    @PreAuthorize("hasRole('LIBRARIAN')") 
    public Transac on returnBook(@RequestParam Long transac onId) { 
        return txService.returnBook(transac onId); 
    } 
 
    @GetMapping("/overdue") 
    public List<Transac on> getOverdue() { 
        return txService.getOverdueBooks(); 
    } 
 
    @GetMapping("/history/{userId}") 
    public List<Transac on> getHistory(@PathVariable Long userId) { 
        return txService.userHistory(userId); 
    } 
} 
 
 
@Configura on 
@EnableWebSecurity 
public class SecurityConfig { 
 
    @Bean 
    public SecurityFilterChain filterChain(H pSecurity h p) throws Excep on { 
        h p.csrf().disable() 
            .authorizeH pRequests() 
                .requestMatchers("/transac ons/**", "/books/**").hasRole("LIBRARIAN") 
                .anyRequest().authen cated() 
            .and() 
            .h pBasic(); 
        return h p.build(); 
    } 
 
    @Bean 
    public UserDetailsService users() { 
        UserDetails librarian = User.withUsername("admin") 
                .password("{noop}admin123") 
                .roles("LIBRARIAN") 
                .build(); 
        return new InMemoryUserDetailsManager(librarian); 
    } 
} 
 
 
Applica on proper es :  
 
spring.datasource.url=jdbc:h2:mem:librarydb 
spring.datasource.driverClassName=org.h2.Driver 
spring.datasource.username=sa 
spring.datasource.password= 
spring.jpa.hibernate.ddl-auto=update 
spring.h2.console.enabled=true 
spring.h2.console.path=/h2-console