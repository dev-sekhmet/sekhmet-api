import React, { useState, useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntities } from './chat-member.reducer';
import { IChatMember } from 'app/shared/model/chat-member.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const ChatMember = (props: RouteComponentProps<{ url: string }>) => {
  const dispatch = useAppDispatch();

  const chatMemberList = useAppSelector(state => state.chatMember.entities);
  const loading = useAppSelector(state => state.chatMember.loading);

  useEffect(() => {
    dispatch(getEntities({}));
  }, []);

  const handleSyncList = () => {
    dispatch(getEntities({}));
  };

  const { match } = props;

  return (
    <div>
      <h2 id="chat-member-heading" data-cy="ChatMemberHeading">
        <Translate contentKey="sekhmetApp.chatMember.home.title">Chat Members</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="sekhmetApp.chatMember.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="sekhmetApp.chatMember.home.createLabel">Create new Chat Member</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {chatMemberList && chatMemberList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="sekhmetApp.chatMember.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="sekhmetApp.chatMember.uid">Uid</Translate>
                </th>
                <th>
                  <Translate contentKey="sekhmetApp.chatMember.scope">Scope</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {chatMemberList.map((chatMember, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${chatMember.id}`} color="link" size="sm">
                      {chatMember.id}
                    </Button>
                  </td>
                  <td>{chatMember.uid}</td>
                  <td>
                    <Translate contentKey={`sekhmetApp.ChatMemberScope.${chatMember.scope}`} />
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`${match.url}/${chatMember.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${chatMember.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${chatMember.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="sekhmetApp.chatMember.home.notFound">No Chat Members found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default ChatMember;
