import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntity } from './chat-member.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const ChatMemberDetail = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getEntity(props.match.params.id));
  }, []);

  const chatMemberEntity = useAppSelector(state => state.chatMember.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="chatMemberDetailsHeading">
          <Translate contentKey="sekhmetApiApp.chatMember.detail.title">ChatMember</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="sekhmetApiApp.chatMember.id">Id</Translate>
            </span>
          </dt>
          <dd>{chatMemberEntity.id}</dd>
          <dt>
            <span id="scope">
              <Translate contentKey="sekhmetApiApp.chatMember.scope">Scope</Translate>
            </span>
          </dt>
          <dd>{chatMemberEntity.scope}</dd>
          <dt>
            <Translate contentKey="sekhmetApiApp.chatMember.chat">Chat</Translate>
          </dt>
          <dd>{chatMemberEntity.chat ? chatMemberEntity.chat.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/chat-member" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/chat-member/${chatMemberEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ChatMemberDetail;
