import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import Chat from './chat';
import ChatDetail from './chat-detail';
import ChatUpdate from './chat-update';
import ChatDeleteDialog from './chat-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={ChatUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={ChatUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={ChatDetail} />
      <ErrorBoundaryRoute path={match.url} component={Chat} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={ChatDeleteDialog} />
  </>
);

export default Routes;
