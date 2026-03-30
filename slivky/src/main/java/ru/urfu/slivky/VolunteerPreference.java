@Entity
@Table(name = "volunteer_preferences")
public class VolunteerPreference {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean worksWithCats = true;
    private Boolean worksWithDogs = true;
    private Boolean worksWithShelters = true;
    private Boolean worksWithPrivate = true;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String availabilitySchedule; // JSONB в виде String или можно использовать Map<String, String>

    // Геттеры и сеттеры
}