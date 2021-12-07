import React from 'react';
import MenuItem from 'app/shared/layout/menus/menu-item';
import { Translate, translate } from 'react-jhipster';
import { NavDropdown } from './menu-components';

export const EntitiesMenu = props => (
  <NavDropdown
    icon="th-list"
    name={translate('global.menu.entities.main')}
    id="entity-menu"
    data-cy="entity"
    style={{ maxHeight: '80vh', overflow: 'auto' }}
  >
    <>{/* to avoid warnings when empty */}</>
    <MenuItem icon="asterisk" to="/chat">
      <Translate contentKey="global.menu.entities.chat" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/message">
      <Translate contentKey="global.menu.entities.message" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/chat-member">
      <Translate contentKey="global.menu.entities.chatMember" />
    </MenuItem>
    {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
  </NavDropdown>
);
