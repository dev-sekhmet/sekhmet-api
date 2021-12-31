import React from 'react';
import 'react-chat-elements/dist/main.css';
import PhotoMessage from 'app/shared/component/photo-message-box';
import VideoMessage from 'app/shared/component/video-message-box';
import AudioMessage from 'app/shared/component/audio-box-message';

export const MessageBox = (props: any) => {
  return (
    <div style={props.isMe ? styles.rightContainer : styles.leftContainer} onClick={props.onClick}>
      {props.title && (
        <div style={styles.boxTitle} onClick={props.onTitleClick}>
          {props.title && <span>{props.title}</span>}
        </div>
      )}
      {props.type === 'photo' && (
        <PhotoMessage
          onOpen={props.onOpen}
          onDownload={props.onDownload}
          onLoad={props.onLoad}
          onPhotoError={props.onPhotoError}
          data={props.data}
          width={props.width}
          height={props.height}
        />
      )}
      {props.type === 'audio' && (
        <AudioMessage onOpen={props.onOpen} onDownload={props.onDownload} onLoad={props.onLoad} data={props.data} text={props.text} />
      )}
      {props.type === 'video' && (
        <VideoMessage
          downloaded={true}
          onOpen={props.onOpen}
          onDownload={props.onDownload}
          onLoad={props.onLoad}
          onVideoError={props.onPhotoError}
          data={props.data}
          width={props.width}
          height={props.height}
        />
      )}
      {props.text && <div>{props.text}</div>}
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
export default MessageBox;
