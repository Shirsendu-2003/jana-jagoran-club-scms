package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.Notice;
import com.janajagoran.scms.enums.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderByCreatedAtDesc();
    List<Notice> findByType(NoticeType type);
}
