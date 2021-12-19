import React, { useState, useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IChat } from 'app/shared/model/chat.model';
import { getEntities as getChats } from 'app/entities/chat/chat.reducer';
import { getEntity, updateEntity, createEntity, reset } from './message.reducer';
import { IMessage } from 'app/shared/model/message.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const MessageUpdate = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const chats = useAppSelector(state => state.chat.entities);
  const messageEntity = useAppSelector(state => state.message.entity);
  const loading = useAppSelector(state => state.message.loading);
  const updating = useAppSelector(state => state.message.updating);
  const updateSuccess = useAppSelector(state => state.message.updateSuccess);
  const handleClose = () => {
    props.history.push('/message' + props.location.search);
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
      ...messageEntity,
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
          ...messageEntity,
          chat: messageEntity?.chat?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="sekhmetApiApp.message.home.createOrEditLabel" data-cy="MessageCreateUpdateHeading">
            <Translate contentKey="sekhmetApiApp.message.home.createOrEditLabel">Create or edit a Message</Translate>
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
                  id="message-id"
                  label={translate('sekhmetApiApp.message.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField label={translate('sekhmetApiApp.message.text')} id="message-text" name="text" data-cy="text" type="text" />
              <ValidatedField
                label={translate('sekhmetApiApp.message.createdAt')}
                id="message-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="date"
              />
              <ValidatedField
                label={translate('sekhmetApiApp.message.image')}
                id="message-image"
                name="image"
                data-cy="image"
                type="text"
              />
              <ValidatedField
                label={translate('sekhmetApiApp.message.video')}
                id="message-video"
                name="video"
                data-cy="video"
                type="text"
              />
              <ValidatedField
                label={translate('sekhmetApiApp.message.audio')}
                id="message-audio"
                name="audio"
                data-cy="audio"
                type="text"
              />
              <ValidatedField
                label={translate('sekhmetApiApp.message.system')}
                id="message-system"
                name="system"
                data-cy="system"
                check
                type="checkbox"
              />
              <ValidatedField
                label={translate('sekhmetApiApp.message.sent')}
                id="message-sent"
                name="sent"
                data-cy="sent"
                check
                type="checkbox"
              />
              <ValidatedField
                label={translate('sekhmetApiApp.message.received')}
                id="message-received"
                name="received"
                data-cy="received"
                check
                type="checkbox"
              />
              <ValidatedField
                label={translate('sekhmetApiApp.message.pending')}
                id="message-pending"
                name="pending"
                data-cy="pending"
                check
                type="checkbox"
              />
              <ValidatedField id="message-chat" name="chat" data-cy="chat" label={translate('sekhmetApiApp.message.chat')} type="select">
                <option value="" key="0" />
                {chats
                  ? chats.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/message" replace color="info">
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

export default MessageUpdate;
