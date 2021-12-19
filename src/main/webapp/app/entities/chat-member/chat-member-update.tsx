import React, { useState, useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IChat } from 'app/shared/model/chat.model';
import { getEntities as getChats } from 'app/entities/chat/chat.reducer';
import { getEntity, updateEntity, createEntity, reset } from './chat-member.reducer';
import { IChatMember } from 'app/shared/model/chat-member.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { ChatMemberScope } from 'app/shared/model/enumerations/chat-member-scope.model';

export const ChatMemberUpdate = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const chats = useAppSelector(state => state.chat.entities);
  const chatMemberEntity = useAppSelector(state => state.chatMember.entity);
  const loading = useAppSelector(state => state.chatMember.loading);
  const updating = useAppSelector(state => state.chatMember.updating);
  const updateSuccess = useAppSelector(state => state.chatMember.updateSuccess);
  const chatMemberScopeValues = Object.keys(ChatMemberScope);
  const handleClose = () => {
    props.history.push('/chat-member' + props.location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(props.match.params.id));
    }

    dispatch(getChats({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...chatMemberEntity,
      ...values,
      chat: chats.find(it => it.id.toString() === values.chat.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          scope: 'PARTICIPANT',
          ...chatMemberEntity,
          chat: chatMemberEntity?.chat?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="sekhmetApiApp.chatMember.home.createOrEditLabel" data-cy="ChatMemberCreateUpdateHeading">
            <Translate contentKey="sekhmetApiApp.chatMember.home.createOrEditLabel">Create or edit a ChatMember</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="chat-member-id"
                  label={translate('sekhmetApiApp.chatMember.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('sekhmetApiApp.chatMember.scope')}
                id="chat-member-scope"
                name="scope"
                data-cy="scope"
                type="select"
              >
                {chatMemberScopeValues.map(chatMemberScope => (
                  <option value={chatMemberScope} key={chatMemberScope}>
                    {translate('sekhmetApiApp.ChatMemberScope.' + chatMemberScope)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="chat-member-chat"
                name="chat"
                data-cy="chat"
                label={translate('sekhmetApiApp.chatMember.chat')}
                type="select"
              >
                <option value="" key="0" />
                {chats
                  ? chats.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/chat-member" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default ChatMemberUpdate;
