require 'sinatra'
require 'sinatra/json'
require 'sinatra/activerecord'
require 'rack'
require 'rack/contrib'
require 'pp'
require 'models/publication'

use Rack::PostBodyContentTypeParser

post '/sendVote' do

  #todo: verify signature
  
  for index in 0 ... params['votes'].size-1
    create = votes.create(vote_value: params['votes'].fetch(index),
                          ballot_box: params['ballot_box'].to_i,
                          serial_number: params['serial_number'].to_i,
                          race_id: index+1)
  end
  
end

post '/publish' do
    publication = Publication.create!( content: params[ "content" ] )
    json id: publication.id
end

post '/publishZKP' do
  
  #todo: verify signature
  
  voteZkp = ZKP.find_by(vote_id: params['vote_id'])
  voteZkp.update("party_#{params[:party_id]}": params['zkp'])
  
  rescue ActiveRecord::RecordNotFound
    create = ZKP.create(vote_id: params['vote_id'].to_i, "party_#{params['party_id']}": params['zkp'])
end

get '/retrieve' do
    begin
        id = params[ "id" ].to_i
        publication = Publication.find id
        json :content => publication.content
    rescue ActiveRecord::RecordNotFound => e
        raise Exception.new( "could not find id=#{id}" )
    end
end
