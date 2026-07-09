package com.github.rrin.entity.process;

import com.github.rrin.entity.item.Item;
import com.github.rrin.entity.item.ItemCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessStep processStep;

    @ManyToOne(fetch = FetchType.EAGER)
    private Item consumed;

    @ManyToOne(fetch = FetchType.EAGER)
    private ItemCategory consumableCategory;

    private int consumedQuantity;

    @PrePersist
    @PreUpdate
    public void checkConsumables(){
        boolean itemDefined = (consumed != null);
        boolean itemCategoryDefined = (consumableCategory != null);

        if (itemDefined == itemCategoryDefined){
            throw new IllegalStateException("Consumable and Item Category are both defined.");
        }
    }
}
