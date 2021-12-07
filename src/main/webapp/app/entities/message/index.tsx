import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import Message from './message';
import MessageDetail from './message-detail';
import MessageUpdate from './message-update';
import MessageDeleteDialog from './message-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={MessageUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={MessageUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={MessageDetail} />
      <ErrorBoundaryRoute path={match.url} component={Message} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={MessageDeleteDialog} />
  </>
);

export default Routes;
