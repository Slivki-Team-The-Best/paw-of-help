@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String category;

    @ManyToMany(mappedBy = "skills")
    private Set<User> users;

    @ManyToMany(mappedBy = "requiredSkills")
    private Set<Task> tasks;

    // Геттеры и сеттеры
}