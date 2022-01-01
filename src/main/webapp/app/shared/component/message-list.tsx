import React from 'react';
import MessageBox from 'app/shared/component/message-box';

export const MessageList = (props: any) => {
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
