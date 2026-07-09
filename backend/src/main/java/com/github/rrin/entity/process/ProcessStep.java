package com.github.rrin.entity.process;

import com.github.rrin.entity.item.Item;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "process_id",
                "sequence"
        })
})
public class ProcessStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Process process;

    private int seq;

    @ManyToOne(fetch = FetchType.EAGER)
    private Operation operation;

    @ManyToOne(fetch = FetchType.EAGER)
    private Item outputItem;

    private int outputQuantity;
}
