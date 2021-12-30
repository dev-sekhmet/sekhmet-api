import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getRoles, getUser, initChat, leaveChat, reset } from './user-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { receiver, sendMessageWebSocket } from 'app/config/websocket-middleware-chat';
import { getChatByUser } from 'app/entities/chat/chat.reducer';
import { createEntityWithMedia, getMessagesByChat, websocketChatMessage } from 'app/entities/message/message.reducer';
import { IMessage } from 'app/shared/model/message.model';
import { Button as ChatButton, Input, MessageList } from 'react-chat-elements';
import 'react-chat-elements/dist/main.css';
import './chat.css';

export const UserManagementChat = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();
  let textInput = null;
  const isInvalid = false;
  const chatEntity = useAppSelector(state => state.chat.entity);

  useEffect(() => {
    dispatch(getUser(props.match.params.id));
    dispatch(getRoles());
    dispatch(getChatByUser(props.match.params.id));
  }, [props.match.params.id]);

  useEffect(() => {
    if (chatEntity.id) {
      dispatch(getMessagesByChat({ query: chatEntity.id }));
      dispatch(initChat(chatEntity.id));
    }
  }, [chatEntity]);

  useEffect(() => {
    if (chatEntity.id) {
      receiver().subscribe(message => {
        return dispatch(websocketChatMessage(message));
      });
    }
  }, [chatEntity]);

  useEffect(() => {
    return () => {
      dispatch(leaveChat(chatEntity.id));
      dispatch(reset());
    };
  }, []);

  const getRefMessageInput = el => {
    if (el != null) {
      textInput = el;
    }
  };

  const processMessage = (event: any) => {
    /* eslint-disable no-console */
    console.log('FILE, ', event.target.files[0]);
    dispatch(
      createEntityWithMedia({
        message: {
          text: textInput.input.value,
          chat: chatEntity,
        },
        file: event.target.files[0],
      })
    );
  };

  const sendMessage = () => {
    /* eslint-disable no-console */
    console.log('MESSAGE, ', textInput.input.value);

    sendMessageWebSocket({
      text: textInput.input.value,
      chat: chatEntity,
    });
  };
  const user = useAppSelector(state => state.userManagement.user);
  const loading = useAppSelector(state => state.userManagement.loading);
  const messages: ReadonlyArray<IMessage> = useAppSelector(state => state.message.entities);

  const sorted = [...messages]
    // TODO: Avoid duplicate useEffect call sevral times
    .filter((m, i, self) => i === self.findIndex(m1 => m1.id === m.id))
    .sort((a, b) => {
      return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
    })
    .map(value => {
      return {
        position: value.user.id === user.id ? 'left' : 'right',
        type: 'text',
        title: value.user.id === user.id ? user.firstName + ' ' + user.lastName : 'Vous',
        // avatar: 'https://i.pravatar.cc/300',
        text: value.text,
        date: new Date(value.createdAt),
      } as any;
    });

  /*  sorted.push({
      position: 'right',
      type: 'photo',
      title: 'Vous',
      data: {
        uri: 'https://i.pravatar.cc/300',
        status: {
          click: false,
          loading: 0,
        }
      },
      text: 'hey regarde ça',
      date: new Date(),
    });*/
  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h1>
            <Translate contentKey="userManagement.home.discussewith" interpolate={{ username: user.firstName + ' ' + user.lastName }}>
              Discussion avec username
            </Translate>
          </h1>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm onSubmit={sendMessage}>
              <MessageList className="message-list" lockable={true} toBottomHeight={'100%'} dataSource={sorted} />
              <Input
                placeholder="Type here..."
                ref={el => getRefMessageInput(el)}
                rightButtons={
                  <>
                    <div className="upload-btn-wrapper">
                      <Input type="file" id="media" onChange={processMessage} />
                      <button className="upload-btn">
                        <img alt="Upload file" src="https://img.icons8.com/metro/26/000000/send-file.png" />
                      </button>
                    </div>
                    <ChatButton color="white" backgroundColor="black" text="Send" />
                  </>
                }
              />
              <Button tag={Link} to="/admin/user-management" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default UserManagementChat;
