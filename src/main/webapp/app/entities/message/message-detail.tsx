import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntity } from './message.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const MessageDetail = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getEntity(props.match.params.id));
  }, []);

  const messageEntity = useAppSelector(state => state.message.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="messageDetailsHeading">
          <Translate contentKey="sekhmetApiApp.message.detail.title">Message</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="sekhmetApiApp.message.id">Id</Translate>
            </span>
          </dt>
          <dd>{messageEntity.id}</dd>
          <dt>
            <span id="text">
              <Translate contentKey="sekhmetApiApp.message.text">Text</Translate>
            </span>
          </dt>
          <dd>{messageEntity.text}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="sekhmetApiApp.message.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>
            {messageEntity.createdAt ? <TextFormat value={messageEntity.createdAt} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="image">
              <Translate contentKey="sekhmetApiApp.message.image">Image</Translate>
            </span>
          </dt>
          <dd>{messageEntity.image}</dd>
          <dt>
            <span id="video">
              <Translate contentKey="sekhmetApiApp.message.video">Video</Translate>
            </span>
          </dt>
          <dd>{messageEntity.video}</dd>
          <dt>
            <span id="audio">
              <Translate contentKey="sekhmetApiApp.message.audio">Audio</Translate>
            </span>
          </dt>
          <dd>{messageEntity.audio}</dd>
          <dt>
            <span id="system">
              <Translate contentKey="sekhmetApiApp.message.system">System</Translate>
            </span>
          </dt>
          <dd>{messageEntity.system ? 'true' : 'false'}</dd>
          <dt>
            <span id="sent">
              <Translate contentKey="sekhmetApiApp.message.sent">Sent</Translate>
            </span>
          </dt>
          <dd>{messageEntity.sent ? 'true' : 'false'}</dd>
          <dt>
            <span id="received">
              <Translate contentKey="sekhmetApiApp.message.received">Received</Translate>
            </span>
          </dt>
          <dd>{messageEntity.received ? 'true' : 'false'}</dd>
          <dt>
            <span id="pending">
              <Translate contentKey="sekhmetApiApp.message.pending">Pending</Translate>
            </span>
          </dt>
          <dd>{messageEntity.pending ? 'true' : 'false'}</dd>
          <dt>
            <Translate contentKey="sekhmetApiApp.message.chat">Chat</Translate>
          </dt>
          <dd>{messageEntity.chat ? messageEntity.chat.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/message" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/message/${messageEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default MessageDetail;
