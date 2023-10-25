package attendance.batch.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.bitbox.bitbox.enums.AttendanceStatus;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "attendance", uniqueConstraints = { @UniqueConstraint(columnNames = { "member_id", "attendance_date" }) })
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
        name = "ATTENDANCE_SEQ_GENERATOR",
        sequenceName = "ATTENDANCE_SEQ", // 매핑할 데이터베이스 시퀀스 이름
        initialValue = 1,
        allocationSize = 50)
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ATTENDANCE_SEQ_GENERATOR")
    @Column(name = "attendance_id")
    private Long attendanceId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "entrace_time")
    private LocalTime entranceTime;

    @Column(name = "quit_time")
    private LocalTime quitTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_state", nullable = false, columnDefinition = "varchar(255) default 'ABSENT'")
    private AttendanceStatus attendanceState;

    @Column(name = "attendance_modify_reason", columnDefinition = "text")
    private String attendanceModifyReason;

    public Attendance(Member member, LocalDate attendanceDate, AttendanceStatus attendanceState) {
        this.member = member;
        this.attendanceDate = attendanceDate;
        this.attendanceState = attendanceState;
    }
}