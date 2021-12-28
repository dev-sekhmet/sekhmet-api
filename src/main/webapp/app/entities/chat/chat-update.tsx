import React, { useEffect, useState } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { createEntity, getEntity, reset, updateEntity } from './chat.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const ChatUpdate = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const chatEntity = useAppSelector(state => state.chat.entity);
  const loading = useAppSelector(state => state.chat.loading);
  const updating = useAppSelector(state => state.chat.updating);
  const updateSuccess = useAppSelector(state => state.chat.updateSuccess);
  const handleClose = () => {
    props.history.push('/chat' + props.location.search);
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
      ...chatEntity,
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
          ...chatEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="sekhmetApiApp.chat.home.createOrEditLabel" data-cy="ChatCreateUpdateHeading">
            <Translate contentKey="sekhmetApiApp.chat.home.createOrEditLabel">Create or edit a Chat</Translate>
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
                  id="chat-id"
                  label={translate('sekhmetApiApp.chat.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField label={translate('sekhmetApiApp.chat.icon')} id="chat-icon" name="icon" data-cy="icon" type="text" />
              <ValidatedField label={translate('sekhmetApiApp.chat.name')} id="chat-name" name="name" data-cy="name" type="text" />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/chat" replace color="info">
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

export default ChatUpdate;
