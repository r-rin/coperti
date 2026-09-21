package com.github.rrin.production;

import com.github.rrin.item.Item;
import com.github.rrin.process.Process;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private ProductionOrder order;

    @ManyToOne
    private Process process;

    private int quantity;

    
}
