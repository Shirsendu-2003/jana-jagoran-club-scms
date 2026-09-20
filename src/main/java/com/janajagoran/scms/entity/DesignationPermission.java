package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.MenuKey;
import jakarta.persistence.*;
import lombok.*;

/** "Menu Allotment" -- which app sections a designation's holder can access. */
@Entity
@Table(name = "designation_permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"post_detail_id", "menu_key"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DesignationPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_detail_id", nullable = false)
    private DesignationPostDetail postDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_key", nullable = false, length = 50)
    private MenuKey menuKey;
}
