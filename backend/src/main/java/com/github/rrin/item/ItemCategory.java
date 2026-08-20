package com.github.rrin.item;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ItemCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ItemCategory> children = new ArrayList<>();

    public void addChild(ItemCategory child) {
        children.add(child);
        child.parent = this;
    }

    public void removeChild(ItemCategory child) {
        children.remove(child);
        child.parent = null;
    }
}
