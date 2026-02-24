package ru.practicum.ewm.main.compilation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.practicum.ewm.main.event.model.Event;

import java.util.Set;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean pinned;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String title;

    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;
}
