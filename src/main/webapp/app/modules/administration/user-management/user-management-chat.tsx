import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { translate, Translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getRoles, getUser, initChat, leaveChat, reset } from './user-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { receiver, sendMessageWebSocket } from 'app/config/websocket-middleware-chat';
import { getByUser } from 'app/entities/chat/chat.reducer';
import { getEntitiesByChat, websocketChatMessage } from 'app/entities/message/message.reducer';

export const UserManagementChat = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();
  const isInvalid = false;
  const user = useAppSelector(state => state.userManagement.user);
  const loading = useAppSelector(state => state.userManagement.loading);
  const updating = useAppSelector(state => state.userManagement.updating);
  const chatEntity = useAppSelector(state => state.chat.entity);
  const messages = useAppSelector(state => state.message.entities);

  useEffect(() => {
    dispatch(getUser(props.match.params.id));
    dispatch(getRoles());
    dispatch(getByUser(props.match.params.id));
    return () => {
      dispatch(reset());
    };
  }, [props.match.params.id]);

  useEffect(() => {
    if (chatEntity.id) {
      dispatch(getEntitiesByChat({ query: chatEntity.id }));
      dispatch(initChat(chatEntity.id));
    }
  }, [user, chatEntity]);

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
    };
  }, []);

  const sendMessage = values => {
    sendMessageWebSocket({
      text: values.text,
      chat: chatEntity,
    });
  };

  const sorted = [...messages].sort((a, b) => {
    return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
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
              <table className="table table-sm table-striped table-bordered">
                <tbody>
                  {sorted &&
                    sorted.map((message, i) => (
                      <tr key={`log-row-${i}`}>
                        <td>{message.user.login}</td>
                        <td>{message.text}</td>
                        <td>{message.createdAt}</td>
                      </tr>
                    ))}
                </tbody>
              </table>
              <ValidatedField type="text" name="text" label={translate('userManagement.message')} />
              <Button tag={Link} to="/admin/user-management" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" type="submit" disabled={isInvalid || updating}>
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
