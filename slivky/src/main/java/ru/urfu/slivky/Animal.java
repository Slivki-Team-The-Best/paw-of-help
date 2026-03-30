@Entity
@Table(name = "animals")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private String type; // DOG, CAT, OTHER

    private String breed;
    private Integer age;

    @Column
    private String gender; // MALE, FEMALE, UNKNOWN

    @Column(columnDefinition = "TEXT")
    private String description;

    private String healthStatus;
    private String specialNeeds;

    @ElementCollection
    private Set<String> photoUrls;

    private String status = "SEEKING_HELP";

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Геттеры и сеттеры
}