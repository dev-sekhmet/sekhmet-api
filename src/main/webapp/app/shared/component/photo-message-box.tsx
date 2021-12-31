import React from 'react';
import 'react-chat-elements/dist/main.css';

export const PhotoMessage = (props: any) => {
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
      <img src={props.data.uri} alt={props.data.alt} onClick={props.onOpen} onLoad={props.onLoad} onError={props.onPhotoError} />
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
export default PhotoMessage;
