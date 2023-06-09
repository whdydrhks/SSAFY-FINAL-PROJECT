package com.ssafy.nanumi.api.service;

import com.ssafy.nanumi.common.ChatRoomInfoDTO;
import com.ssafy.nanumi.common.CreateChatRoomDTO;
import com.ssafy.nanumi.config.response.exception.CustomException;
import com.ssafy.nanumi.config.response.exception.CustomExceptionStatus;
import com.ssafy.nanumi.db.entity.*;
import com.ssafy.nanumi.db.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessageSendingOperations messageTemplate;
    private final ChatRepository chatRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    BlacklistRepository blacklistRepository;
    //TODO 채팅방 생성 메서드
    @Transactional
    public ResponseEntity<?> CreateChatRoom(CreateChatRoomDTO DTO) {
        long sendUser = DTO.getSendUser(); // 나 자신
        long receiveUser = DTO.getOpponentId(); // 상대방
        long productId = DTO.getProductId(); // 상품 아이디

        Match match = matchRepository.findMatchByProductAndUsers(productId, receiveUser)
                .orElseThrow(() -> new CustomException(CustomExceptionStatus.NOT_FOUND_MATCH) );
        match.setMatching(true);

        // Check if the chatroom already exists
        List<ChatRoomEntity> existingChatRooms = chatRoomRepository.findByOpponentIdAndProductId(receiveUser, productId);
        if (!existingChatRooms.isEmpty()) {
            throw new CustomException(CustomExceptionStatus.NOT_FOUND_CHAT_ROOM);
        }

        long[] users = new long[]{sendUser, receiveUser}; // 나랑 상대방 배열에 저장

        try {
            ChatRoomEntity chatRoomEntity = ChatRoomEntity.builder()
                    .userList(users)
                    .opponentId(DTO.getOpponentId())
                    .opponentNickname(DTO.getOpponentNickname())
                    .opponentProfileImage(DTO.getOpponentProfileImage())
                    .productId(DTO.getProductId())
                    .build();

            chatRoomRepository.save(chatRoomEntity);

            messageTemplate.convertAndSend("/sub/user/" + sendUser, new com.ssafy.nanumi.common.SubscribeChatRoomDTO("CHATROOM", receiveUser, chatRoomEntity.getChatroomSeq()));
            messageTemplate.convertAndSend("/sub/user/" + receiveUser, new com.ssafy.nanumi.common.SubscribeChatRoomDTO("CHATROOM", sendUser, chatRoomEntity.getChatroomSeq()));

            return new ResponseEntity<>(Collections.singletonMap("productId", chatRoomEntity.getProductId()), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create chat room", HttpStatus.BAD_REQUEST);
        }
    }


    // TODO 모든 채팅방 찾기 메소드
    public List<ChatRoomInfoDTO> FindMyChatRooms(long user) {
        List<ChatRoomEntity> chatRoomEntities = chatRoomRepository.findAllByUserListContaining(user);
        List<ChatRoomInfoDTO> chatRoomInfoDTOs = new ArrayList<>();

        for (ChatRoomEntity chatRoomEntity : chatRoomEntities) {
            ChatRoomInfoDTO chatRoomInfoDTO = new ChatRoomInfoDTO();

            long opponentId = Arrays.stream(chatRoomEntity.getUserList())
                    .filter(id -> id != user)
                    .findFirst()
                    .orElse(0L);

            // 차단 목록 true/ false 리턴
//            List<Blacklist> blockedUsers = blacklistRepository.findByBlockerIdAndIsBlockedTrue(user);
            List<Long> blacklists = new ArrayList<>();

            List<Long> blockers = blacklistRepository.findBlockerId(user);
            for (long blocker : blockers) {
                blacklists.add(blocker);
            }

            List<Long> targets = blacklistRepository.findTargetId(user);
            for (long target : targets) {
                blacklists.add(target);
            }

            boolean isOpponentBlocked = blacklists.stream().anyMatch(blacklist -> blacklist == opponentId);
            chatRoomInfoDTO.setBlocked(isOpponentBlocked);

            // 상대방 정보를 UserRepository에서 가져오기 (MySQL)
            User opponent = userRepository.findById(opponentId).orElse(null);
            if (opponent != null) {
                chatRoomInfoDTO.setOpponentNickname(opponent.getNickname());
                chatRoomInfoDTO.setOpponentProfileImage(opponent.getProfileUrl());
                chatRoomInfoDTO.setOpponentId(opponentId);
            } else {
                System.err.println("Opponent not found with ID: " + opponentId);
                continue; // 다음 chatRoomEntity로 이동
            }

            // 마지막 메시지 정보 가져오기
            List<ChatMessageEntity> lastMessages = chatRepository.findTop1ByRoomIdOrderBySendTimeDesc(chatRoomEntity.getChatroomSeq());
            ChatMessageEntity lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0);
            chatRoomInfoDTO.setChatRoomId(chatRoomEntity.getChatroomSeq());
            chatRoomInfoDTO.setProductId(chatRoomEntity.getProductId());
            if (lastMessage != null) {
                chatRoomInfoDTO.setLastMessage(lastMessage.getMessage());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd a hh:mm ss:SSS");
                chatRoomInfoDTO.setLastMessageTime(LocalDateTime.parse(lastMessage.getSendTime(), formatter));
            }

            chatRoomInfoDTOs.add(chatRoomInfoDTO);
        }

        return chatRoomInfoDTOs;
    }


    // TODO 채팅방 신고처리 메서드
    @Transactional
    public boolean reportByRoomSeq(Long seq) {
        ChatRoomEntity chatRoom = chatRoomRepository.findChatRoomEntityByChatroomSeq(seq);

        // 찾은 채팅방이 없다면 false 리턴
        if (chatRoom == null) return false;

        // 채팅방의 활성화 상태를 false로 변경하여 비활성화 합니다.
        chatRoom.setActivate(false);

        // 변경된 정보를 저장한다.
        chatRoomRepository.save(chatRoom);

        // 채팅방 신고처리가 성공적으로 완료되었으므로 true를 반환한다.
        return true;
    }

}

