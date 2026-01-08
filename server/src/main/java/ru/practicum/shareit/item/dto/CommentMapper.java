package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * Маппер для преобразования между Comment и DTO.
 */
public class CommentMapper {
    /**
     * Преобразует CommentDto в Comment.
     *
     * @param commentDto DTO комментария
     * @param item       вещь, к которой относится комментарий
     * @param author     автор комментария
     * @return объект комментария
     */
    public static Comment toComment(CommentDto commentDto, Item item, User author) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(commentDto.getCreated() != null ? commentDto.getCreated() : LocalDateTime.now());
        return comment;
    }

    /**
     * Преобразует Comment в CommentDto.
     *
     * @param comment объект комментария
     * @return DTO комментария
     */
    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}