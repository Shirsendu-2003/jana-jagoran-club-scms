package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.EventRequest;
import com.janajagoran.scms.entity.Event;
import com.janajagoran.scms.entity.EventRegistration;
import com.janajagoran.scms.entity.Member;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.exception.BadRequestException;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.EventRegistrationRepository;
import com.janajagoran.scms.repository.EventRepository;
import com.janajagoran.scms.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Event createEvent(EventRequest request, User createdBy) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .imageUrl(request.getImageUrl())
                .createdBy(createdBy)
                .build();
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long id, EventRequest request) {
        Event event = getEvent(id);
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setEventTime(request.getEventTime());
        if (request.getImageUrl() != null) event.setImageUrl(request.getImageUrl());
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) throw new ResourceNotFoundException("Event not found");
        eventRepository.deleteById(id);
    }

    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    public List<Event> getUpcomingEvents() {
        return eventRepository.findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate.now());
    }

    /** Most recent past events (those with an image are the ones worth showcasing publicly). */
    public List<Event> getPastEvents() {
        return eventRepository.findAllByOrderByEventDateDesc().stream()
                .filter(ev -> ev.getEventDate().isBefore(LocalDate.now()))
                .toList();
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAllByOrderByEventDateDesc();
    }

    @Transactional
    public EventRegistration registerForEvent(Long eventId, Long userId) {
        Event event = getEvent(eventId);
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));

        if (registrationRepository.findByEventIdAndMemberId(eventId, member.getId()).isPresent()) {
            throw new BadRequestException("You are already registered for this event");
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .member(member)
                .build();
        return registrationRepository.save(registration);
    }

    public List<EventRegistration> getRegistrations(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    @Transactional
    public void markAttendance(Long registrationId, boolean attended) {
        EventRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
        reg.setAttended(attended);
        registrationRepository.save(reg);
    }
}
