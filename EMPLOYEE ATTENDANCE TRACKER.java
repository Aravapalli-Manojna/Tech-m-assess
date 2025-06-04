Maven Dependencies (pom.xml) 
 
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
        <groupId>org.postgresql</groupId> 
        <ar factId>postgresql</ar factId> 
        <scope>runme</scope> 
    </dependency> 
 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-valida on</ar factId> 
    </dependency> 
</dependencies> 
 
 
@En ty 
public class Employee { 
    @Id 
    @GeneratedValue(strategy = Genera onType.IDENTITY) 
    private Long employeeId; 
 
    private String name; 
    private String department; 
    private String designa on; 
} 
 
@En ty 
public class A endance { 
    @Id 
    @GeneratedValue(strategy = Genera onType.IDENTITY) 
    private Long a endanceId; 
 
    @ManyToOne 
    private Employee employee; 
 
    private LocalDate date; 
 
    @Enumerated(EnumType.STRING) 
    private Status status; 
 
    public enum Status { PRESENT, ABSENT } 
} 
 
 
public interface EmployeeRepository extends JpaRepository<Employee, Long> {} 
 
public interface A endanceRepository extends JpaRepository<A endance, Long> { 
    boolean existsByEmployeeAndDate(Employee employee, LocalDate date); 
 
    List<A endance> findByEmployeeEmployeeId(Long employeeId); 
 
    @Query("SELECT a.employee.department, COUNT(a) FROM A endance a WHERE a.status 
= 'PRESENT' GROUP BY a.employee.department") 
    List<Object[]> departmentSummary(); 
} 
 
 
@Service 
public class A endanceService { 
    @Autowired private A endanceRepository a endanceRepo; 
    @Autowired private EmployeeRepository employeeRepo; 
 
    public A endance markA endance(Long empId, A endance.Status status) { 
        Employee employee = employeeRepo.findById(empId).orElseThrow(); 
        LocalDate today = LocalDate.now(); 
 
        if (a endanceRepo.existsByEmployeeAndDate(employee, today)) { 
            throw new RunmeExcep on("A endance already marked today."); 
        } 
 
        A endance a = new A endance(); 
        a .setEmployee(employee); 
        a .setDate(today); 
        a .setStatus(status); 
        return a endanceRepo.save(a ); 
    } 
 
    public List<A endance> getEmployeeReport(Long empId) { 
        return a endanceRepo.findByEmployeeEmployeeId(empId); 
    } 
 
    public List<Object[]> getDepartmentReport() { 
        return a endanceRepo.departmentSummary(); 
    } 
} 
 
 
@RestController 
@RequestMapping("/a endance") 
public class A endanceController { 
    @Autowired private A endanceService service; 
 
    @PostMapping("/mark") 
    @PreAuthorize("hasRole('EMPLOYEE')") 
    public ResponseEn ty<?> mark(@RequestParam Long empId, @RequestParam String 
status) { 
        A endance.Status st = A endance.Status.valueOf(status.toUpperCase()); 
        return ResponseEn ty.ok(service.markA endance(empId, st)); 
    } 
 
    @GetMapping("/employee/{id}") 
    @PreAuthorize("hasRole('MANAGER')") 
    public List<A endance> employeeReport(@PathVariable Long id) { 
        return service.getEmployeeReport(id); 
    } 
 
    @GetMapping("/summary/department") 
    @PreAuthorize("hasRole('MANAGER')") 
    public List<Object[]> departmentSummary() { 
        return service.getDepartmentReport(); 
    } 
} 
 
 
@Configura on 
@EnableWebSecurity 
public class SecurityConfig { 
 
    @Bean 
    public SecurityFilterChain filterChain(H pSecurity h p) throws Excep on { 
        h p.csrf().disable() 
            .authorizeH pRequests() 
                .requestMatchers("/a endance/mark").hasRole("EMPLOYEE") 
                .requestMatchers("/a endance/summary/**", 
"/a endance/employee/**").hasRole("MANAGER") 
                .anyRequest().authen cated() 
            .and() 
            .h pBasic(); 
        return h p.build(); 
    } 
 
    @Bean 
    public UserDetailsService users() { 
        UserDetails employee = User.withUsername("emp1") 
            .password("{noop}emp123") 
            .roles("EMPLOYEE") 
            .build(); 
 
        UserDetails manager = User.withUsername("mgr1") 
            .password("{noop}mgr123") 
            .roles("MANAGER") 
            .build(); 
 
        return new InMemoryUserDetailsManager(employee, manager); 
    } 
} 
 
 
Applica on Proper es :- 
 
spring.datasource.url=jdbc:postgresql://localhost:5432/a endance_db 
spring.datasource.username=postgres 
spring.datasource.password=yourpassword 
spring.jpa.hibernate.ddl-auto=update 
spring.jpa.show-sql=true