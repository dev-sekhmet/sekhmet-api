import React, { useState } from 'react';
import MessageBox from 'app/shared/component/message-box';

export const MessageList = (props: any) => {
  const [downButton, setDownButton] = useState(false);

  return (
    <div>
      <div>
        {props.dataSource.map((x, i) => (
          <MessageBox key={i} {...x} />
        ))}
      </div>
    </div>
  );
};

export default MessageList;
