import React from 'react';
import 'react-chat-elements/dist/main.css';

export const AudioMessage = (props: any) => {
  return (
    <div
      onClick={props.onClick}
      style={
        props.data.width &&
        props.data.height && {
          width: props.data.width,
          height: props.data.height,
        }
      }
    >
      <audio controls>
        <source src={props.data.uri} type={props.data.contentTypeMedia} />
        Your browser does not support the audio element.
      </audio>
    </div>
  );
};

const styles = {
  row: {
    flexDirection: 'row',
    alignItems: 'flex-end',
  },
  messageReply: {
    backgroundColor: 'gray',
    padding: 5,
    borderRadius: 5,
  },
  leftContainer: {
    padding: 10,
    margin: 10,
    borderRadius: 15,
    maxWidth: '75%',
    backgroundColor: 'lightblue',
    marginLeft: 10,
    marginRight: 'auto',
  },
  rightContainer: {
    padding: 10,
    margin: 10,
    borderRadius: 15,
    maxWidth: '75%',
    backgroundColor: 'lightgray',
    marginLeft: 'auto',
    marginRight: 10,
    alignItems: 'flex-end',
  },
  boxTitle: {
    fontWeight: 500,
    fontSize: 13,
    color: '#4f81a1',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
  },
};
export default AudioMessage;
