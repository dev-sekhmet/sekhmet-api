import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntity } from './chat.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const ChatDetail = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getEntity(props.match.params.id));
  }, []);

  const chatEntity = useAppSelector(state => state.chat.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="chatDetailsHeading">
          <Translate contentKey="sekhmetApp.chat.detail.title">Chat</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{chatEntity.id}</dd>
          <dt>
            <span id="guid">
              <Translate contentKey="sekhmetApp.chat.guid">Guid</Translate>
            </span>
          </dt>
          <dd>{chatEntity.guid}</dd>
          <dt>
            <span id="icon">
              <Translate contentKey="sekhmetApp.chat.icon">Icon</Translate>
            </span>
          </dt>
          <dd>{chatEntity.icon}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="sekhmetApp.chat.name">Name</Translate>
            </span>
          </dt>
          <dd>{chatEntity.name}</dd>
        </dl>
        <Button tag={Link} to="/chat" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/chat/${chatEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ChatDetail;
