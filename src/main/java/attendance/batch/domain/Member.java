package attendance.batch.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "member")
@NoArgsConstructor
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private String memberId;

    @Column(name = "class_id")
    private Long classId;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Attendance> attendances;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "member_nickname", nullable = false)
    private String memberNickname;

    @Column(name = "member_email", nullable = false)
    private String memberEmail;

    @Column(name = "member_profile_img", nullable = false)
    private String memberProfileImg;

    @Column(name = "member_credit", nullable = false, columnDefinition = "bigint default 0")
    private Long memberCredit;

    @Column(name = "member_authority", nullable = false)
    private String memberAuthority;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted;

    public Member(String memberId, Long classId, String memberName, String memberNickname, String memberEmail, String memberProfileImg, Long memberCredit, String memberAuthority, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isDeleted) {
        this.memberId = memberId;
        this.classId = classId;
        this.memberName = memberName;
        this.memberNickname = memberNickname;
        this.memberEmail = memberEmail;
        this.memberProfileImg = memberProfileImg;
        this.memberCredit = memberCredit;
        this.memberAuthority = memberAuthority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }
}