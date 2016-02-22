require 'sinatra'
require 'sinatra/json'
require 'sinatra/activerecord'
require 'rack'
require 'rack/contrib'
require 'pp'
require 'models/publication'

use Rack::PostBodyContentTypeParser

post '/sendVote' do
    json :parameters_were => params
end

post '/publish' do
    publication = Publication.create!( content: params[ "content" ] )
    json id: publication.id
end

post '/publishZKP' do
  data = JSON.parse request.body.read
  
  #todo: verify signature
  
  voteZkp = ZKP.find(data[:vote_id])
    update = ZKP.update(data[:vote_id], :"party_#{data[:party_id]}" => data[:zkp])
  
rescue ActiveRecord::RecordNotFound
  create = ZKP.create(vote_id: data[:vote_id], "party_#{data[:party_id]}": data[:zkp])
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
