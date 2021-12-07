import React, { useState, useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntity, updateEntity, createEntity, reset } from './chat-member.reducer';
import { IChatMember } from 'app/shared/model/chat-member.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { ChatMemberScope } from 'app/shared/model/enumerations/chat-member-scope.model';

export const ChatMemberUpdate = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const chatMemberEntity = useAppSelector(state => state.chatMember.entity);
  const loading = useAppSelector(state => state.chatMember.loading);
  const updating = useAppSelector(state => state.chatMember.updating);
  const updateSuccess = useAppSelector(state => state.chatMember.updateSuccess);
  const chatMemberScopeValues = Object.keys(ChatMemberScope);
  const handleClose = () => {
    props.history.push('/chat-member');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(props.match.params.id));
    }
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
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="sekhmetApp.chatMember.home.createOrEditLabel" data-cy="ChatMemberCreateUpdateHeading">
            <Translate contentKey="sekhmetApp.chatMember.home.createOrEditLabel">Create or edit a ChatMember</Translate>
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
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('sekhmetApp.chatMember.uid')}
                id="chat-member-uid"
                name="uid"
                data-cy="uid"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('sekhmetApp.chatMember.scope')}
                id="chat-member-scope"
                name="scope"
                data-cy="scope"
                type="select"
              >
                {chatMemberScopeValues.map(chatMemberScope => (
                  <option value={chatMemberScope} key={chatMemberScope}>
                    {translate('sekhmetApp.ChatMemberScope.' + chatMemberScope)}
                  </option>
                ))}
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
