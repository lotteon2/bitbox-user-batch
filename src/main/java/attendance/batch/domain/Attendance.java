package attendance.batch.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Time;

// ALTER TABLE `attendance` ADD UNIQUE `키명` ( `member_id` , `ATTENDANCE_DATE` )
// 위의 alter가 가능은 한데 JPA는 알 수 없으므로 예외로 처리 됨
@Entity
@Table(name = "attendance")
@NoArgsConstructor
@Getter
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "attendance_date", nullable = false)
    private Date attendanceDate;

    @Column(name = "entrance_time")
    private Time entranceTime;

    @Column(name = "quit_time")
    private Time quitTime;

    @Column(name = "attendance_state", nullable = false, columnDefinition = "varchar(255) default '결석'")
    private String attendanceState;

    @Column(name = "attendance_modify_reason", columnDefinition = "text")
    private String attendanceModifyReason;

    public Attendance(Member member, Date attendanceDate, String attendanceState) {
        this.member = member;
        this.attendanceDate = attendanceDate;
        this.attendanceState = attendanceState;
    }
}