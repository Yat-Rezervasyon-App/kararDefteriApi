
package com.tein.karardefteri.dev;

import com.tein.karardefteri.decision.entity.Decision;
import com.tein.karardefteri.decision.repository.DecisionRepository;
import com.tein.karardefteri.decision.entity.DecisionStatus;
import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import com.tein.karardefteri.project.entity.Project;
import com.tein.karardefteri.project.repository.ProjectRepository;
import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.entity.TaskPriority;
import com.tein.karardefteri.task.entity.TaskStatus;
import com.tein.karardefteri.task.repository.TaskRepository;
import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@Profile("dev")
@ConditionalOnProperty(
        prefix = "app.seed",
        name = "enabled",
        havingValue = "true"
)
public class DevelopmentDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MeetingRepository meetingRepository;
    private final DecisionRepository decisionRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public DevelopmentDataSeeder(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            MeetingRepository meetingRepository,
            DecisionRepository decisionRepository,
            TaskRepository taskRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.meetingRepository = meetingRepository;
        this.decisionRepository = decisionRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.count() > 0) {
            System.out.println("Development seed atlandı: Kullanıcılar zaten mevcut.");
            return;
        }

        User admin = createUser(
                "Demo Admin",
                "admin@demo.local",
                "Admin123!",
                UserRole.ADMIN
        );

        User memberOne = createUser(
                "Demo Kullanıcı 1",
                "member1@demo.local",
                "Member123!",
                UserRole.MEMBER
        );

        User memberTwo = createUser(
                "Demo Kullanıcı 2",
                "member2@demo.local",
                "Member123!",
                UserRole.MEMBER
        );

        Project projectOne = createProject(
                "KararDefteri Web",
                "Web uygulaması geliştirme projesi",
                admin
        );

        Project projectTwo = createProject(
                "Mobil Uygulama",
                "Mobil uygulama geliştirme projesi",
                admin
        );

        Meeting meetingOne = createMeeting(
                projectOne,
                admin,
                "Web Tasarım Toplantısı"
        );

        Meeting meetingTwo = createMeeting(
                projectOne,
                admin,
                "API Geliştirme Toplantısı"
        );

        Meeting meetingThree = createMeeting(
                projectTwo,
                admin,
                "Mobil Arayüz Toplantısı"
        );

        Meeting meetingFour = createMeeting(
                projectTwo,
                admin,
                "Test ve Yayına Alma Toplantısı"
        );

        createDecision(meetingOne, "Ana sayfa tasarımı hazırlanacak.");
        createDecision(meetingTwo, "API testleri tamamlanacak.");
        createDecision(meetingThree, "Mobil ekran tasarımları gözden geçirilecek.");
        createDecision(meetingFour, "Yayın öncesi kontroller yapılacak.");

        taskRepository.saveAll(List.of(
                createTask(meetingOne, "Ana sayfa tasarımını hazırla",
                        memberOne, -5, TaskStatus.IN_PROGRESS, TaskPriority.HIGH),

                createTask(meetingOne, "Renk paletini belirle",
                        memberTwo, 5, TaskStatus.TODO, TaskPriority.LOW),

                createTask(meetingTwo, "API dokümantasyonunu tamamla",
                        memberOne, -2, TaskStatus.DONE, TaskPriority.MEDIUM),

                createTask(meetingTwo, "Görev filtrelerini kontrol et",
                        null, null, TaskStatus.TODO, TaskPriority.HIGH),

                createTask(meetingThree, "Mobil giriş ekranını hazırla",
                        memberTwo, -3, TaskStatus.IN_PROGRESS, TaskPriority.HIGH),

                createTask(meetingThree, "Profil ekranını tasarla",
                        null, 7, TaskStatus.TODO, TaskPriority.MEDIUM),

                createTask(meetingFour, "Test senaryolarını yaz",
                        memberOne, 3, TaskStatus.TODO, TaskPriority.MEDIUM),

                createTask(meetingFour, "Yayın kontrol listesini tamamla",
                        memberTwo, -1, TaskStatus.DONE, TaskPriority.LOW)
        ));

        System.out.println("Development seed başarıyla oluşturuldu.");
    }

    private User createUser(
            String fullName,
            String email,
            String password,
            UserRole role) {

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);

        return userRepository.save(user);
    }

    private Project createProject(
            String name,
            String description,
            User admin) {

        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setCreatedBy(admin.getId());

        return projectRepository.save(project);
    }

    private Meeting createMeeting(
            Project project,
            User admin,
            String title) {

        Meeting meeting = new Meeting();
        meeting.setProjectId(project.getId());
        meeting.setTitle(title);
        meeting.setMeetingAt(OffsetDateTime.now());
        meeting.setDescription(title + " için örnek toplantı.");
        meeting.setCreatedBy(admin.getId());

        return meetingRepository.save(meeting);
    }

    private void createDecision(Meeting meeting, String content) {

        Decision decision = new Decision();
        decision.setMeetingId(meeting.getId());
        decision.setContent(content);
        decision.setStatus(DecisionStatus.ACTIVE);

        decisionRepository.save(decision);
    }

    private Task createTask(
            Meeting meeting,
            String title,
            User assignee,
            Integer dueDays,
            TaskStatus status,
            TaskPriority priority) {

        Task task = new Task();
        task.setMeetingId(meeting.getId());
        task.setTitle(title);
        task.setDescription(title + " için örnek görev.");

        if (assignee != null) {
            task.setAssigneeId(assignee.getId());
        }

        if (dueDays != null) {
            task.setDueDate(OffsetDateTime.now().plusDays(dueDays));
        }

        task.setStatus(status);
        task.setPriority(priority);

        return task;
    }
}
