post '/publishMessage' do
    demand_valid_signature!( request )
    publication = Message.create! party_id:       params[ "party_id" ],
                                recepient_id:   params[ "recepient_id" ],
                                message:        params[ "message" ],
                                signature:      params[ "signature" ]
end

get '/retrieveMessage' do
    r = if params[ "recepient_id" ]
        Message.where( recepient_id: params[ "recepient_id" ] ).to_json
    else
        Message.all.to_json
    end
end
