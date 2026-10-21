package com.github.rrin.inventory;

import com.github.rrin.item.Item;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/** What an other-use request withdraws; held here until approval turns each line into a movement. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherUseLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "other_use_id", nullable = false)
    private OtherUse otherUse;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "from_location_id", nullable = false)
    private Location fromLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QualityState quality;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal qty;
}
