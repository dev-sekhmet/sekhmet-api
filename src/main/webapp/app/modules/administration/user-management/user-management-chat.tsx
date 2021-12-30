import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { translate, Translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getRoles, getUser, initChat, leaveChat, reset } from './user-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { receiver, sendMessageWebSocket } from 'app/config/websocket-middleware-chat';
import { getChatByUser } from 'app/entities/chat/chat.reducer';
import { getMessagesByChat, websocketChatMessage } from 'app/entities/message/message.reducer';
import { IMessage } from 'app/shared/model/message.model';
import { MessageList } from 'react-chat-elements';
import 'react-chat-elements/dist/main.css';

export const UserManagementChat = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();
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

  const sendMessage = values => {
    sendMessageWebSocket({
      text: values.text,
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
      };
    });
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
              <ValidatedField type="text" name="text" label={translate('userManagement.message')} />
              <Button tag={Link} to="/admin/user-management" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" type="submit" disabled={isInvalid}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="userManagement.action.sendMessage">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default UserManagementChat;
