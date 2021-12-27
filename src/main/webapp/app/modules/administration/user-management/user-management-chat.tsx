import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { translate, Translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getMessages, getRoles, getUser, reset } from './user-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { sendMessageWebSocket } from 'app/config/websocket-middleware-chat';

export const UserManagementChat = (props: RouteComponentProps<{ login: string }>) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getUser(props.match.params.login));
    dispatch(getMessages(props.match.params.login));
    dispatch(getRoles());
    return () => {
      dispatch(reset());
    };
  }, [props.match.params.login]);

  const handleClose = () => {
    props.history.push('/admin/user-management');
  };

  const sendMessage = values => {
    // eslint-disable-next-line no-console
    console.log('values ', values);
    sendMessageWebSocket(values);
  };

  const isInvalid = false;
  const user = useAppSelector(state => state.userManagement.user);
  const loading = useAppSelector(state => state.userManagement.loading);
  const updating = useAppSelector(state => state.userManagement.updating);
  const messages = useAppSelector(state => state.userManagement.messages);

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
            <ValidatedForm onSubmit={sendMessage} defaultValues={{ message: 'samplemessage' }}>
              <table className="table table-sm table-striped table-bordered">
                <tbody>
                  {messages.map((activity, i) => (
                    <tr key={`log-row-${i}`}>
                      <td>{activity.userLogin}</td>
                      <td>{activity.message}</td>
                      <td>{activity.time}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <ValidatedField type="text" name="message" label={translate('userManagement.message')} />
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
