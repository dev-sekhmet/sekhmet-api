import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import ChatMember from './chat-member';
import ChatMemberDetail from './chat-member-detail';
import ChatMemberUpdate from './chat-member-update';
import ChatMemberDeleteDialog from './chat-member-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={ChatMemberUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={ChatMemberUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={ChatMemberDetail} />
      <ErrorBoundaryRoute path={match.url} component={ChatMember} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={ChatMemberDeleteDialog} />
  </>
);

export default Routes;
