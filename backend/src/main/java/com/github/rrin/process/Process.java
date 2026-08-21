package com.github.rrin.process;

import com.github.rrin.item.Item;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"produced_id", "version"}
        )
})
public class Process {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produced_id")
    private Item produces;

    private int version;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "process", fetch = FetchType.EAGER)
    private List<ProcessStep> steps;
}
