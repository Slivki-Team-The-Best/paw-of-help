@Entity
@Table(name = "shelters")
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Type(type = "org.hibernate.spatial.GeometryType")
    private org.locationtech.jts.geom.Point location;

    private String address;
    private String phone;
    private String email;
    private String website;
    private Boolean verified = false;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "shelter")
    private Set<Animal> animals;

    // Геттеры и сеттеры
}