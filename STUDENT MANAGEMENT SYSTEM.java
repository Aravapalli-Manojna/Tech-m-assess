Dependencies (Maven pom.xml) 
 
<dependencies> 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-thymeleaf</ar factId> 
    </dependency> 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-web</ar factId> 
    </dependency> 
 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-data-jpa</ar factId> 
    </dependency> 
    <dependency> 
        <groupId>com.h2database</groupId> 
        <ar factId>h2</ar factId> 
        <scope>runme</scope> 
    </dependency> 
 
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <ar factId>spring-boot-starter-valida on</ar factId> 
    </dependency> 
</dependencies> 
 
 
@En ty 
public class Student { 
 
    @Id 
    @GeneratedValue(strategy = Genera onType.IDENTITY) 
    private Long studentId; 
 
    @NotBlank(message = "Name is required") 
    private String name; 
 
    @Min(value = 5, message = "Minimum age is 5") 
    @Max(value = 100, message = "Maximum age is 100") 
    private int age; 
 
    @NotBlank(message = "Class is required") 
    private String studentClass; 
 
    @Email(message = "Invalid email") 
    @NotBlank(message = "Email is required") 
    private String email; 
 
    private String address; 
 
} 
 
 
public interface StudentRepository extends JpaRepository<Student, Long> { 
    Page<Student> 
findByNameContainingIgnoreCaseOrStudentClassContainingIgnoreCase(String name, String 
studentClass, Pageable pageable); 
} 
 
 
@Service 
public class StudentService { 
 
    @Autowired 
    private StudentRepository repository; 
 
    public Page<Student> listAll(String keyword, Pageable pageable) { 
        if (keyword != null && !keyword.isEmpty()) { 
            return 
repository.findByNameContainingIgnoreCaseOrStudentClassContainingIgnoreCase(keyword, 
keyword, pageable); 
        } 
        return repository.findAll(pageable); 
    } 
 
    public Student get(Long id) { 
        return repository.findById(id).orElseThrow(() -> new NoSuchElementExcep on("Student 
not found")); 
    } 
 
    public void save(Student student) { 
        repository.save(student); 
    } 
 
    public void delete(Long id) { 
        repository.deleteById(id); 
    } 
} 
 
 
@Controller 
@RequestMapping("/students") 
public class StudentController { 
 
    @Autowired 
    private StudentService service; 
 
    @GetMapping 
    public String viewHomePage(Model model, 
                                @RequestParam(defaultValue = "0") int page, 
                                @RequestParam(required = false) String keyword) { 
        Page<Student> students = service.listAll(keyword, PageRequest.of(page, 5)); 
        model.addA ribute("students", students); 
        model.addA ribute("keyword", keyword); 
        return "students"; 
    } 
 
    @GetMapping("/new") 
    public String showNewStudentForm(Model model) { 
        model.addA ribute("student", new Student()); 
        return "student_form"; 
    } 
 
    @PostMapping("/save") 
    public String saveStudent(@Valid @ModelA ribute("student") Student student, 
                              BindingResult result, Model model) { 
        if (result.hasErrors()) { 
            return "student_form"; 
        } 
        service.save(student); 
        return "redirect:/students"; 
    } 
 
    @GetMapping("/edit/{id}") 
    public String editStudent(@PathVariable Long id, Model model) { 
        model.addA ribute("student", service.get(id)); 
        return "student_form"; 
    } 
 
    @GetMapping("/delete/{id}") 
    public String deleteStudent(@PathVariable Long id) { 
        service.delete(id); 
        return "redirect:/students"; 
    } 
} 
 
 
<!DOCTYPE html> 
<html xmlns:th="h p://www.thymeleaf.org"> 
<head> 
    < tle>Student List</ tle> 
</head> 
<body> 
<h2>Student List</h2> 
<form method="get" ac on="#" th:ac on="@{/students}"> 
    <input type="text" name="keyword" placeholder="Search by name/class" 
th:value="${keyword}"> 
    <bu on type="submit">Search</bu on> 
</form> 
<a th:href="@{/students/new}">Add New Student</a> 
<table border="1"> 
    <thead> 
        
<tr><th>ID</th><th>Name</th><th>Class</th><th>Age</th><th>Email</th><th>Ac ons</t
 h></tr> 
    </thead> 
    <tbody> 
        <tr th:each="student : ${students.content}"> 
            <td th:text="${student.studentId}"></td> 
            <td th:text="${student.name}"></td> 
            <td th:text="${student.studentClass}"></td> 
            <td th:text="${student.age}"></td> 
            <td th:text="${student.email}"></td> 
            <td> 
                <a th:href="@{/students/edit/{id}(id=${student.studentId})}">Edit</a> 
                <a th:href="@{/students/delete/{id}(id=${student.studentId})}" onclick="return 
confirm('Delete?')">Delete</a> 
            </td> 
        </tr> 
    </tbody> 
</table> 
<div> 
    <a th:if="${students.hasPrevious()}" th:href="@{/students(page=${students.number - 1}, 
keyword=${keyword})}">Prev</a> 
    Page [[${students.number + 1}]] of [[${students.totalPages}]] 
    <a th:if="${students.hasNext()}" th:href="@{/students(page=${students.number + 1}, 
keyword=${keyword})}">Next</a> 
</div> 
</body> 
</html> 
 
 
<!DOCTYPE html> 
<html xmlns:th="h p://www.thymeleaf.org"> 
<head>< tle>Student Form</ tle></head> 
<body> 
<h2>Student Form</h2> 
<form th:ac on="@{/students/save}" th:object="${student}" method="post"> 
    <input type="hidden" th:field="*{studentId}" /> 
    <label>Name:</label> 
    <input type="text" th:field="*{name}" /> 
    <div th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></div> 
 
    <label>Age:</label> 
    <input type="number" th:field="*{age}" /> 
    <div th:if="${#fields.hasErrors('age')}" th:errors="*{age}"></div> 
 
    <label>Class:</label> 
    <input type="text" th:field="*{studentClass}" /> 
    <div th:if="${#fields.hasErrors('studentClass')}" th:errors="*{studentClass}"></div> 
 
    <label>Email:</label> 
<input type="email" th:field="*{email}" /> 
<div th:if="${#fields.hasErrors('email')}" th:errors="*{email}"></div> 
<label>Address:</label> 
<input type="text" th:field="*{address}" /> 
<bu on type="submit">Save</bu on> 
</form> 
</body> 
</html> 
Applica on Proper es :  
spring.datasource.url=jdbc:h2:mem:studentsdb 
spring.datasource.driverClassName=org.h2.Driver 
spring.datasource.username=sa 
spring.datasource.password= 
spring.jpa.hibernate.ddl-auto=update 
spring.h2.console.enabled=true 
spring.h2.console.path=/h2-console 