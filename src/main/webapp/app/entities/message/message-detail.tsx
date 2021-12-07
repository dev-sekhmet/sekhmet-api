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
          <Translate contentKey="sekhmetApp.message.detail.title">Message</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{messageEntity.id}</dd>
          <dt>
            <span id="uid">
              <Translate contentKey="sekhmetApp.message.uid">Uid</Translate>
            </span>
          </dt>
          <dd>{messageEntity.uid}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="sekhmetApp.message.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>
            {messageEntity.createdAt ? <TextFormat value={messageEntity.createdAt} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="image">
              <Translate contentKey="sekhmetApp.message.image">Image</Translate>
            </span>
          </dt>
          <dd>{messageEntity.image}</dd>
          <dt>
            <span id="video">
              <Translate contentKey="sekhmetApp.message.video">Video</Translate>
            </span>
          </dt>
          <dd>{messageEntity.video}</dd>
          <dt>
            <span id="audio">
              <Translate contentKey="sekhmetApp.message.audio">Audio</Translate>
            </span>
          </dt>
          <dd>{messageEntity.audio}</dd>
          <dt>
            <span id="system">
              <Translate contentKey="sekhmetApp.message.system">System</Translate>
            </span>
          </dt>
          <dd>{messageEntity.system ? 'true' : 'false'}</dd>
          <dt>
            <span id="sent">
              <Translate contentKey="sekhmetApp.message.sent">Sent</Translate>
            </span>
          </dt>
          <dd>{messageEntity.sent ? 'true' : 'false'}</dd>
          <dt>
            <span id="received">
              <Translate contentKey="sekhmetApp.message.received">Received</Translate>
            </span>
          </dt>
          <dd>{messageEntity.received ? 'true' : 'false'}</dd>
          <dt>
            <span id="pending">
              <Translate contentKey="sekhmetApp.message.pending">Pending</Translate>
            </span>
          </dt>
          <dd>{messageEntity.pending ? 'true' : 'false'}</dd>
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
