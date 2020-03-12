package community.comment.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import community.comment.domain.Comment;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CommentResponseDto {
  private Long id;
  private String userKey;
  private String content;
  private Long parentId;
  private int order;
  private boolean active;
  private List<CommentResponseDto> children = new ArrayList<>();
  @JsonFormat
      (shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
  private ZonedDateTime createdAt;

  private CommentResponseDto(Comment comment) {
    this.id = comment.getId();
    this.userKey = comment.getUserKey();
    this.content = comment.getContent();
    this.parentId = comment.getParentId();
    this.active = comment.isActive();
    this.order = comment.getOrder();
    this.createdAt = comment.getCreatedAt();
  }

  public static CommentResponseDto of(Comment comment) {
    return new CommentResponseDto(comment);
  }

  public void addChild(CommentResponseDto comment) {
    children.add(comment);
  }
}

