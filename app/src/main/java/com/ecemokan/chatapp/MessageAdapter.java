package com.ecemokan.chatapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);


            senderMessageText =  itemView.findViewById(R.id.sender_messsage_text);
            senderMessageText.setTextColor(Color.WHITE);
            receiverMessageText =  itemView.findViewById(R.id.receiver_message_text);
            receiverMessageText.setTextColor(Color.WHITE);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.no_profile).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.WHITE);
                messageViewHolder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
        }
        else if(fromMessageType.equals("image"))
        {
            if(fromUserID.equals(messageSenderId))
            {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);

            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);

            }
        }

        else if(fromMessageType.equals("pdf") || fromMessageType.equals("docx"))
        {
            if(fromUserID.equals(messageSenderId))
            {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.file);


            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.file);

            }
        }

        if(fromUserID.equals(messageSenderId))
        {
          messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                  {
                      CharSequence options[] =new CharSequence[]
                              {
                                      "Delete for me",
                                      "Download and view this document",
                                      "Cancel",
                                      "Delete for everyone"
                              };
                      AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                      builder.setTitle("Delete message?");

                      builder.setItems(options, new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {

                              if(which ==0)
                              {
                                  deleteSentMessage(position,messageViewHolder);
                                  Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                  messageViewHolder.itemView.getContext().startActivity(intent);
                              }
                              else if(which ==1)
                              {

                                          Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                          messageViewHolder.itemView.getContext().startActivity(intent);

                              }
                              else if(which ==3)
                              {
                                deleteMessageForEveryOne(position,messageViewHolder);
                                  Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                  messageViewHolder.itemView.getContext().startActivity(intent);
                              }
                          }
                      });
                      builder.show();
                  }

                  else if(userMessagesList.get(position).getType().equals("text"))
                  {
                      CharSequence options[] =new CharSequence[]
                              {
                                      "Delete for me",
                                      "Cancel",
                                      "Delete for everyone"
                              };
                      AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                      builder.setTitle("Delete message?");

                      builder.setItems(options, new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {

                              if(which ==0)
                              {
                                  deleteSentMessage(position,messageViewHolder);
                                  Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                  messageViewHolder.itemView.getContext().startActivity(intent);
                              }

                              else if(which ==2)
                              {
                                  deleteMessageForEveryOne(position,messageViewHolder);
                                  Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                  messageViewHolder.itemView.getContext().startActivity(intent);
                              }

                          }
                      });
                      builder.show();
                  }

                  else if(userMessagesList.get(position).getType().equals("image"))
                  {
                      CharSequence options[] =new CharSequence[]
                              {
                                      "Delete for me",
                                      "View this image",
                                      "Cancel",
                                      "Delete for everyone"
                              };
                      AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                      builder.setTitle("Delete message?");

                      builder.setItems(options, new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {

                              if(which ==0)
                              {
                                  deleteSentMessage(position,messageViewHolder);
                                  Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                  messageViewHolder.itemView.getContext().startActivity(intent);
                              }
                              else if(which ==1)
                              {
                                  Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                  intent.putExtra("url",userMessagesList.get(position).getMessage());
                                  messageViewHolder.itemView.getContext().startActivity(intent);
                              }
                              else if(which ==3)
                              {
                                  deleteMessageForEveryOne(position,messageViewHolder);
                                  Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                  messageViewHolder.itemView.getContext().startActivity(intent);
                              }

                          }
                      });
                      builder.show();
                  }


              }
          });
        }
        else
        {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] =new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and view this document",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which ==0)
                                {
                                    deleteReceiveMessages(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(which ==1)
                                {

                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }

                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text"))
                     {
                        CharSequence options[] =new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which ==0)
                                {
                                    deleteReceiveMessages(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] =new CharSequence[]
                                {
                                        "Delete for me",
                                        "View this image",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which ==0)
                                {
                                    deleteReceiveMessages(position,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(which ==1)
                                {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }


                }
            });
        }
    }




    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }


    private void deleteSentMessage(final int position,final MessageViewHolder holder)
    {
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted successfully.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error occured.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deleteReceiveMessages(final int position,final MessageViewHolder holder)
    {
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted successfully.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error occured.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deleteMessageForEveryOne(final int position,final MessageViewHolder holder)
    {
        final DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Deleted successfully.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error occured.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}

